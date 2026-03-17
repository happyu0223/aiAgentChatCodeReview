[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

param(
    [string]$ApiUrl = "http://localhost:8080/api/code-review/submit",
    [int]$UserId = 1
)

$ErrorActionPreference = "Continue"

Write-Host "Checking staged files..." -ForegroundColor Cyan

$stagedFiles = git diff --cached --name-only --diff-filter=ACM 2>$null

if (-not $stagedFiles) {
    Write-Host "No staged files found" -ForegroundColor Green
    exit 0
}

$langMap = @{
    "java" = "java"
    "py" = "python"
    "js" = "javascript"
    "jsx" = "javascript"
    "ts" = "typescript"
    "tsx" = "typescript"
    "vue" = "vue"
    "go" = "go"
    "cs" = "csharp"
    "cpp" = "cpp"
    "c" = "c"
    "rb" = "ruby"
    "php" = "php"
}

$hasBlockingIssue = $false

foreach ($file in $stagedFiles) {
    Write-Host "`nChecking: $file" -ForegroundColor Yellow
    
    $ext = [System.IO.Path]::GetExtension($file).TrimStart(".").ToLower()
    
    if (-not $langMap.ContainsKey($ext)) {
        Write-Host "  Skipped (unsupported: $ext)" -ForegroundColor Gray
        continue
    }
    
    $language = $langMap[$ext]
    
    try {
        $content = git show ":$file" 2>$null
        if (-not $content) {
            continue
        }
        
        $body = @{
            projectName = "git-commit"
            fileName = $file
            language = $language
            codeContent = $content
            reviewType = "git"
        } | ConvertTo-Json -Compress
        
        $headers = @{
            "X-User-Id" = $UserId
            "Content-Type" = "application/json"
        }
        
        Write-Host "  Calling AI review..." -ForegroundColor Gray
        
        $response = Invoke-RestMethod -Uri $ApiUrl -Method Post -Body $body -Headers $headers -TimeoutSec 120 -ErrorAction Stop
        
        if ($response.code -eq 200 -and $response.data.result) {
            $result = $response.data.result
            $summary = $result.summary
            
            if ($result.rawResponse) {
                try {
                    $jsonMatch = [regex]::Match($result.rawResponse, '\{[\s\S]*\}')
                    if ($jsonMatch.Success) {
                        $parsed = $jsonMatch.Value | ConvertFrom-Json
                        if ($parsed.summary) {
                            $summary = $parsed.summary
                        }
                    }
                } catch {}
            }
            
            $totalIssues = if ($summary.totalIssues) { $summary.totalIssues } else { 0 }
            $critical = if ($summary.critical) { $summary.critical } else { 0 }
            $major = if ($summary.major) { $summary.major } else { 0 }
            
            if ($totalIssues -gt 0) {
                Write-Host "  Found $totalIssues issues (critical: $critical, major: $major)" -ForegroundColor Yellow
                
                if ($result.issues) {
                    foreach ($issue in $result.issues) {
                        $color = switch ($issue.severity) {
                            "critical" { "Red" }
                            "major" { "Yellow" }
                            default { "White" }
                        }
                        Write-Host "    [$($issue.severity)] $($issue.title): $($issue.message)" -ForegroundColor $color
                    }
                }
                
                if ($critical -gt 0) {
                    $hasBlockingIssue = $true
                }
            } else {
                Write-Host "  PASSED" -ForegroundColor Green
            }
        }
    } catch {
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
if ($hasBlockingIssue) {
    Write-Host "BLOCKED: Critical issues found, commit rejected" -ForegroundColor Red
    Write-Host "Please fix critical issues before committing" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "DONE: Code check completed" -ForegroundColor Green
    exit 0
}
