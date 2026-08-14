# Build stage
FROM node:20-alpine AS build
WORKDIR /build
COPY package.json package-lock.json ./
# ng2-charts declares an unbounded peer dep ("@angular/cdk>=17.0.0"), so a
# clean install resolves it to the latest cdk (21.x) which conflicts with
# this project's Angular 18 - pre-existing issue, invisible locally because
# node_modules there predates it. --legacy-peer-deps skips that strict check.
RUN npm ci --legacy-peer-deps
COPY . .
RUN npm run build -- --configuration production

# Runtime stage
FROM nginx:alpine
# Angular 18's esbuild-based "application" builder outputs client files under
# a "browser" subfolder (dist/material-management/browser). If this ever
# changes (builder/config update), the COPY below will fail loudly at build
# time rather than silently serving nothing - fix the path here.
COPY --from=build /build/dist/material-management/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
