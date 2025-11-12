@echo off
echo ====================================================
echo 🧹 Cleaning Git history to remove large JAR files...
echo ====================================================

:: Step 1. Check if git-filter-repo is installed
where git-filter-repo >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ git-filter-repo not found.
    echo Installing via pip...
    pip install git-filter-repo
)

:: Step 2. Remove the large JAR file from entire history
echo Removing large file from Git history...
git filter-repo --path notification-service/target/notification-service-0.0.1-SNAPSHOT.jar --invert-paths

:: Step 3. Add .gitignore rules
echo Updating .gitignore...
(
    echo # Ignore build outputs
    echo */target/
    echo *.jar
) >> .gitignore

:: Step 4. Commit the .gitignore update
git add .gitignore
git commit -m "Ignore build outputs and clean large JARs from history"

:: Step 5. Force push the cleaned repository
echo ====================================================
echo 🚀 Force pushing cleaned history to GitHub...
echo ====================================================
git push origin main --force

echo ====================================================
echo ✅ Done! Large files removed and repo pushed successfully.
echo ====================================================
pause
