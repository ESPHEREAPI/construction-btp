import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { CompanyService } from '../../../core/services/company.service';
import { Company, CreateCompanyRequest } from '../../../core/models/company.model';
import { CompanyLicenseModalComponent } from '../company-license-modal/company-license-modal.component';
import { CompanyDetailModalComponent } from '../company-detail-modal/company-detail-modal.component';

@Component({
  selector: 'app-company-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule, CompanyLicenseModalComponent, CompanyDetailModalComponent],
  templateUrl: './company-list.component.html',
  styleUrls: ['./company-list.component.scss']
})
export class CompanyListComponent implements OnInit {
  companies: Company[] = [];
  loading = true;
  error = '';

  showCreateForm = false;
  creating = false;
  createError = '';
  newCompany: CreateCompanyRequest = {
    companyName: '',
    adminUsername: '',
    adminEmail: '',
    adminPassword: '',
    adminFirstName: '',
    adminLastName: ''
  };

  companyForLicenseModal: Company | null = null;
  companyForDetailModal: Company | null = null;

  constructor(private companyService: CompanyService) {}

  ngOnInit(): void {
    this.loadCompanies();
  }

  loadCompanies(): void {
    this.loading = true;
    this.companyService.getAllCompanies().subscribe({
      next: (data) => {
        this.companies = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des compagnies';
        this.loading = false;
        console.error(err);
      }
    });
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    this.createError = '';
  }

  createCompany(): void {
    if (!this.newCompany.companyName || !this.newCompany.adminUsername || !this.newCompany.adminEmail || !this.newCompany.adminPassword) {
      this.createError = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    this.creating = true;
    this.createError = '';

    this.companyService.createCompany(this.newCompany).subscribe({
      next: () => {
        this.creating = false;
        this.showCreateForm = false;
        this.newCompany = { companyName: '', adminUsername: '', adminEmail: '', adminPassword: '', adminFirstName: '', adminLastName: '' };
        this.loadCompanies();
      },
      error: (err) => {
        this.creating = false;
        this.createError = err?.error?.message || 'Erreur lors de la création';
      }
    });
  }

  toggleStatus(company: Company): void {
    this.companyService.setStatus(company.id, !company.active).subscribe({
      next: () => this.loadCompanies(),
      error: () => alert('Erreur lors du changement de statut')
    });
  }

  openLicenseModal(company: Company): void {
    this.companyForLicenseModal = company;
  }

  closeLicenseModal(): void {
    this.companyForLicenseModal = null;
  }

  openDetailModal(company: Company): void {
    this.companyForDetailModal = company;
  }

  closeDetailModal(): void {
    this.companyForDetailModal = null;
  }
}
