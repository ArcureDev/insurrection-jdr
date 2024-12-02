import { effect, Injectable, resource, signal } from '@angular/core';
import { toHttpParams } from './utils/object.utils';
import { Game } from './types';

export const api = (value: string) => `/api/${value}`;
export const apiWithParams = <T extends { [key in string]: any }>(
  value: string,
  params: T,
) => {
  const formattedParams = toHttpParams(params);
  return `/api/${value}?${formattedParams}`;
};

@Injectable({
  providedIn: 'root',
})
export class HttpService {
  private eventSource?: EventSource;

  currentGame = signal<Game | undefined>(undefined);

  constructor() {
    this.currentGame = resource({
      loader: () => this.sweetFetch<Game, void>(api('games/me/current')),
    }).value;

    effect(() => {
      const currentGame = this.currentGame();
      if (!currentGame) return;
      this.subscribeToGameUpdates();
    });
  }

  subscribeToGameUpdates() {
    if (this.eventSource) return;
    // this.eventSource = new EventSource('/api/games/sse');
    // this.eventSource.onmessage = (event: MessageEvent<string>) => {
    //   const game = JSON.parse(event.data) as Game;
    //   this.currentGame.set(game);
    // };
  }

  async sweetFetch<T, R>(
    url: string,
    method: 'POST' | 'GET' | 'PUT' | 'DELETE' = 'GET',
    body?: R,
  ): Promise<T> {
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });
    if (!response.ok) {
      throw new Error();
    }
    return (await response.json()) as Promise<T>;
  }

  async logout(): Promise<void> {
    return fetch(api('logout'), {
      method: 'POST',
    }).then(() => {
      this.eventSource = undefined;
    });
  }
}
