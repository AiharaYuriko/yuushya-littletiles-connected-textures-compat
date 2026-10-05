param(
    [Parameter(Mandatory=$true)][string]$ClasspathFile,
    [string]$JdkPath = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
if (-not $JdkPath) { throw 'Provide JDK 21 using -JdkPath or JAVA_HOME.' }
$dependencies = @([IO.File]::ReadAllLines((Resolve-Path -LiteralPath $ClasspathFile).Path) | Where-Object { $_.Trim() })
foreach ($dependency in $dependencies) { if (-not (Test-Path -LiteralPath $dependency)) { throw "Missing dependency: $dependency" } }
$classpath = $dependencies -join ';'
$output = Join-Path $PSScriptRoot 'build/offline'
New-Item -ItemType Directory -Path $output -Force | Out-Null
$runDir = Join-Path $output ([Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $runDir | Out-Null
function Compile([string]$sourceDir, [string]$destination, [string]$cp) {
    New-Item -ItemType Directory -Path $destination -Force | Out-Null
    $arguments = @('--release','21','-encoding','UTF-8','-proc:none','-classpath',('"' + $cp.Replace('\','/') + '"'),'-d',('"' + $destination.Replace('\','/') + '"'))
    $arguments += Get-ChildItem -LiteralPath $sourceDir -Recurse -File -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' }
    $argsFile = $destination + '.args'
    [IO.File]::WriteAllLines($argsFile, $arguments)
    & (Join-Path $JdkPath 'bin/javac.exe') "@$argsFile"
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
}
$main = Join-Path $runDir 'main'
Compile (Join-Path $PSScriptRoot 'src/main/java') $main $classpath
foreach ($test in @(
    @('YuushyaLittleTilesConnectedTexturesCompat','com.yuushya.compat.connected.ModeSelectionRegression'),
    @('YuushyaLittleTilesCtmCompat','com.yuushya.compat.ctm.NativeEmitterRegression'),
    @('YuushyaLittleTilesFusionCompat','com.yuushya.compat.fusion.FusionCompatRegression')
)) {
    $testOutput = Join-Path $runDir $test[0]
    Compile (Join-Path $PSScriptRoot ('regression/' + $test[0] + '/java')) $testOutput ($main + ';' + $classpath)
    $argsFile = $testOutput + '.java.args'
    [IO.File]::WriteAllLines($argsFile, @('-classpath',('"' + ($testOutput + ';' + $main + ';' + $classpath).Replace('\','/') + '"'),$test[1]))
    & (Join-Path $JdkPath 'bin/java.exe') "@$argsFile"
    if ($LASTEXITCODE -ne 0) { throw 'Regression failed.' }
}
$jar = Join-Path $output 'yuushya-lt-connected-textures-compat-0.2.6-mc1.21.1.jar'
& (Join-Path $JdkPath 'bin/jar.exe') "-J-Djava.io.tmpdir=$runDir" --create --file $jar -C $main . -C (Join-Path $PSScriptRoot 'src/main/resources') .
if ($LASTEXITCODE -ne 0) { throw 'Packaging failed.' }
Write-Output "Built: $jar"
