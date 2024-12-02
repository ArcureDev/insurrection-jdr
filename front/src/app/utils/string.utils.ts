export function isBlank(value?: string | Date): boolean {
  return value instanceof Date ? !value : (value ?? '').trim().length === 0;
}
