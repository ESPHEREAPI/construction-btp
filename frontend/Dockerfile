# Build stage
FROM node:20-alpine AS build
WORKDIR /build
COPY package.json package-lock.json ./
RUN npm ci
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
