import { HttpParams } from '@angular/common/http';
import { isBlank } from './string.utils';
import { Params } from '@angular/router';

export function isNullOrUndefined<T>(it: T): boolean {
  return it == undefined && it == null;
}

export function isNotNullOrUndefined<T>(it: T): boolean {
  return it !== undefined && it !== null;
}

export function isNotEmpty<T>(value: T | null | undefined): value is T {
  return value !== null && value !== undefined;
}

export function isEmptyObject(obj: any) {
  return obj && Object.keys(obj).length === 0;
}

export function compareObject<T extends { id: number }>(a: T, b: T): boolean {
  return a && b && a.id === b.id;
}

export function parseLink(link?: string): string {
  if (!link) return '';
  return link?.startsWith('www') ? 'https://' + link : link;
}

export function getDate(date?: Date | string): string {
  return new Date(date ?? new Date()).toISOString().split('T')[0];
}

export const isType = <T>(it: any): it is T => {
  return (it as T) !== undefined;
};

export function toHttpParams<T extends { [key in string]: any }>(
  params: T,
  defaultValues?: Partial<T>,
): HttpParams {
  let formattedParams: HttpParams = new HttpParams();
  Object.entries(params).forEach(([key, value]) => {
    let enhanceValue = value ?? defaultValues?.[key];
    if (enhanceValue !== null && enhanceValue !== undefined) {
      if (Array.isArray(enhanceValue)) {
        enhanceValue = enhanceValue.filter(
          (value) => value !== null && value !== undefined,
        );
        formattedParams = enhanceValue.reduce(
          (acc: any, item: any) => acc.append(key, item),
          formattedParams,
        );
      } else {
        formattedParams = formattedParams.set(key, enhanceValue);
      }
    }
  });
  return formattedParams;
}
