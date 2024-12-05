import { Component, input } from '@angular/core';

@Component({
  selector: 'ins-token',
  imports: [],
  templateUrl: './token.component.html',
  styleUrl: './token.component.scss',
})
export class TokenComponent {
  absolute = input<boolean>(true);
  size = input.required<number>();
  index = input.required<number>();
  color = input.required<string>(); //hexa
}
