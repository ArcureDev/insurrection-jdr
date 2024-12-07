export type RGB = { red: number; green: number; blue: number };

export function hexToRgb(hex?: string): RGB {
  if (!hex) return { red: 0, blue: 0, green: 0 };
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result
    ? {
        red: parseInt(result[1], 16),
        green: parseInt(result[2], 16),
        blue: parseInt(result[3], 16),
      }
    : { red: 0, blue: 0, green: 0 };
}
