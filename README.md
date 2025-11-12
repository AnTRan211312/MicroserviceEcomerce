mv# 1. Build tất cả services
mvn clean package -DskipTests

# 2. Start tất cả
docker compose up -d --build

# 3. Xem logs
docker compose logs -f

# 4. Kiểm tra services
docker compose ps



# Eureka Dashboard
# Mở browser: http://localhost:8761


mvn clean package -DskipTests

docker compose down -v

docker compose up -d --build

docker logs product-service --tail 40


docker compose down --rmi 'local'




using batth
java -jar bfg-1.15.0.jar --delete-files "*.jar"
java -jar bfg.jar --delete-files "*.jar"
git reflog expire --expire=now --all
git gc --prune=now --aggressive
git push origin main --force


