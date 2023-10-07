export default function getEnv(name: string, defaultValue: string): string {
  return process.env[name] ?? defaultValue;
}
