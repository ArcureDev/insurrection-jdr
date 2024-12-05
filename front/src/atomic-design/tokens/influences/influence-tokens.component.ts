import { Component, input, signal } from '@angular/core';
import { Player, TokenType } from '../../../app/types';
import { DefaultTokensComponent } from '../default-tokens.component';

@Component({
  selector: 'ins-influence-tokens',
  imports: [DefaultTokensComponent],
  template: '<ins-default-tokens [player]="player()" [type]="type()"/>',
})
export class InfluenceTokensComponent {
  player = input.required<Player | undefined>();

  type = signal<TokenType>('INFLUENCE');
}
