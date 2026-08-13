package com.construction.material.service.impl;

import com.construction.material.dto.request.ChangePasswordRequest;
import com.construction.material.dto.request.CreateCompanyWithAdminRequest;
import com.construction.material.dto.request.LoginRequest;
import com.construction.material.dto.request.SignupRequest;
import com.construction.material.dto.response.CompanyResponse;
import com.construction.material.dto.response.JwtResponse;
import com.construction.material.dto.response.MessageResponse;
import com.construction.material.entity.User;
import com.construction.material.exception.BusinessException;
import com.construction.material.repository.UserRepository;
import com.construction.material.security.CustomUserDetails;
import com.construction.material.security.jwt.JwtUtils;
import com.construction.material.service.AuthService;
import com.construction.material.service.CompanyService;
import com.construction.material.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private LicenseService licenseService;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .preferredLanguage(user.getPreferredLanguage())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .build();
    }

    @Override
    public MessageResponse registerUser(SignupRequest signupRequest) {
        if (!licenseService.isSelfRegistrationEnabled()) {
            throw new BusinessException(messageSource.getMessage("registration.disabled", null, LocaleContextHolder.getLocale()));
        }

        CompanyResponse company = companyService.createCompanyWithAdmin(CreateCompanyWithAdminRequest.builder()
                .companyName(signupRequest.getCompanyName())
                .selfRegistered(true)
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword())
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .phone(signupRequest.getPhone())
                .preferredLanguage(signupRequest.getPreferredLanguage())
                .build());

        return new MessageResponse(messageSource.getMessage("company.created", new Object[]{company.getName()}, LocaleContextHolder.getLocale()));
    }

    @Override
    public MessageResponse changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("user.not.found", null, LocaleContextHolder.getLocale())));

        if (!encoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(messageSource.getMessage("auth.password.current.invalid", null, LocaleContextHolder.getLocale()));
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        return new MessageResponse(messageSource.getMessage("auth.password.changed", null, LocaleContextHolder.getLocale()));
    }
}
