# Production image (Railway/GHCR): static build served by nginx, which also proxies /api to the backend.
# Local development uses Dockerfile.dev.
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# relative, so the browser calls the nginx below (same origin) instead of the backend directly
ARG VITE_API_URL=/api
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build


FROM nginx:1.27-alpine

COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

# Railway sets PORT; BACKEND_URL is the backend's private address (see DEPLOY.md)
ENV PORT=80 \
    BACKEND_URL=http://backend:8080 \
    NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1

EXPOSE 80
