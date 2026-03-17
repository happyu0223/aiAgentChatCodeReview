@echo off
chcp 65001 >nul
echo 正在检查提交代码...

set API_URL=http://localhost:8080/api/code-review/submit
set USER_ID=1

git diff --cached --name-only --diff-filter=ACM > %TEMP%\staged_files.txt
set STAGED_FILES=
set /p STAGED_FILES=<%TEMP%\staged_files.txt

if "%STAGED_FILES%"=="" (
    echo 没有暂存的代码文件
    exit /b 0
)

for %%f in (%STAGED_FILES%) do (
    call :check_file "%%f"
)

echo 代码检查完成
exit /b 0

:check_file
set "FILE=%~1"
echo 检查文件: %FILE%

set "EXTENSION=%FILE:*.=%"

if "%EXTENSION%"=="%FILE%" goto end
if "%EXTENSION%"=="java" set "LANGUAGE=java"
if "%EXTENSION%"=="py" set "LANGUAGE=python"
if "%EXTENSION%"=="js" set "LANGUAGE=javascript"
if "%EXTENSION%"=="vue" set "LANGUAGE=vue"
if "%EXTENSION%"=="ts" set "LANGUAGE=javascript"
if "%EXTENSION%"=="go" set "LANGUAGE=go"

if not defined LANGUAGE goto end

git show ":%FILE%" > %TEMP%\file_content.txt 2>nul
set /p CONTENT=<%TEMP%\file_content.txt

if not defined CONTENT goto end

echo 调用AI审查...

powershell -Command "$content = Get-Content '%TEMP%\file_content.txt' -Raw; $body = @{projectName='git-commit';fileName='%FILE%';language='%LANGUAGE%';codeContent=$content;reviewType='git'} | ConvertTo-Json -Compress; $response = Invoke-RestMethod -Uri '%API_URL%' -Method Post -ContentType 'application/json' -Headers @{'X-User-Id'='%USER_ID%'} -Body $body; Write-Output $response"

:end
exit /b 0
