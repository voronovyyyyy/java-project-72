import { spawn } from 'node:child_process';

const windows = process.platform === 'win32';
const command = windows ? 'gradlew.bat' : './gradlew';
const app = spawn(command, ['run'], {
  stdio: 'inherit',
  shell: windows,
  env: { ...process.env, APP_ENV: 'development' },
});
app.on('exit', code => process.exit(code ?? 1));
