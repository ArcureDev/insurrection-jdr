import { Component, input } from '@angular/core';
import { Player, Token, TokenType } from '../../../app/types';
import { hexToRgb } from '../../../app/utils/utils';
import { JsonPipe } from '@angular/common';

@Component({
  selector: 'ins-token',
  imports: [JsonPipe],
  templateUrl: './token.component.html',
  styleUrl: './token.component.scss',
})
export class TokenComponent {
  absolute = input<boolean>(true);
  size = input.required<number>();
  type = input.required<TokenType>();
  color = input<string>(); //hexa

  protected readonly hexToRgb = hexToRgb;
}
