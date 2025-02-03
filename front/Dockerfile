# Stage 1: Build Environment
FROM node:lts-alpine AS build-stage
# Install build tools (e.g., Maven, Gradle)
# Copy source code
# Build commands (e.g., compile, package)
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Runtime environment
FROM nginx AS final-stage
#  Copy application artifacts from the build stage (e.g., JAR file)
COPY --from=build-stage dist/front/browser/ /usr/share/nginx/html
COPY conf-nginx/nginx.conf /etc/nginx/conf.d/default.conf
