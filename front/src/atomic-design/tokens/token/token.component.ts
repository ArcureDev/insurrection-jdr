import { Component, input } from '@angular/core';
import { TokenType } from '../../../app/types';
import { hexToRgb } from '../../../app/utils/utils';

@Component({
  selector: 'ins-token',
  imports: [],
  templateUrl: './token.component.html',
  styleUrl: './token.component.scss',
})
export class TokenComponent {
  size = input.required<number>();
  type = input.required<TokenType>();
  color = input<string>(); //hexa
}
