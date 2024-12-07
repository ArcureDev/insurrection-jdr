import { Component, effect, input, signal } from '@angular/core';
import { Player, Token, TokenType } from '../../app/types';
import { TokenComponent } from './token/token.component';

@Component({
  selector: 'ins-default-tokens',
  imports: [TokenComponent],
  templateUrl: './default-tokens.component.html',
  styleUrl: './default-tokens.component.scss',
})
export class DefaultTokensComponent {
  player = input.required<Player | undefined>();
  type = input.required<TokenType>();

  title = signal<string | undefined>(undefined);
  tokens = signal<Token[]>([]);

  constructor() {
    effect(() => {
      const player = this.player();
      const type = this.type();
      if (!player || !type) return;

      const shardTokens = player.playableTokens.filter(
        (token) => token.type === 'SHARD',
      );
      const influenceTokens = player.playableTokens.filter(
        (token) => token.type === 'INFLUENCE',
      );

      const title =
        type === 'SHARD'
          ? shardTokens.length + " jetons d'éclat"
          : influenceTokens.length + " jetons d'influence";
      this.title.set(title);

      const tokens = type === 'SHARD' ? shardTokens : influenceTokens;
      this.tokens.set(tokens);
    });
  }
}
