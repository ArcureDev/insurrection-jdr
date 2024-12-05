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

      const title =
        type === 'SHARD'
          ? player.playableTokens.length + " jetons d'éclat"
          : player.myTokens.length + " jetons d'influence";
      this.title.set(title);

      const tokens =
        type === 'SHARD'
          ? player.playableTokens.filter((token) => token.type === type)
          : player.playableTokens.filter((token) => token.type !== 'SHARD');
      this.tokens.set(tokens);
    });
  }
}
