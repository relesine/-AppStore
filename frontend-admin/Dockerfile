# 管理端前端镜像（vite build → nginx 静态托管）
# 构建：docker build -t appstore-web-admin:latest .（在 frontend-admin 目录内）
FROM node:20-alpine AS builder
WORKDIR /build
COPY package.json package-lock.json* ./
RUN npm ci --registry=https://registry.npmmirror.com || npm install --registry=https://registry.npmmirror.com
COPY . .
ARG VITE_API_BASE=/
RUN npm run build

FROM nginx:1.25-alpine
RUN rm -f /etc/nginx/conf.d/default.conf
COPY --from=builder /build/dist /usr/share/nginx/html
COPY nginx-default.conf /etc/nginx/conf.d/appstore.conf

EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost/healthz || exit 1
CMD ["nginx", "-g", "daemon off;"]
