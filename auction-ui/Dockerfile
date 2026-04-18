FROM node:20-alpine as builder
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
# We need the API URL to be baked in or available at runtime
ENV NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080
RUN npm run build

FROM node:20-alpine
WORKDIR /app
COPY --from=builder /app ./
EXPOSE 3000
CMD ["npm", "start"]
