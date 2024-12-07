import { Component, input, output } from '@angular/core';

export type Position = 'start' | 'center' | 'end';

@Component({
  imports: [],
  template: '',
})
export abstract class AbstractButtonComponent {
  title = input.required<string>();
  type = input<'button' | 'submit'>('button');
  fontPosition = input<Position>('center');
  fullWidth = input<boolean>(false);
  disabled = input<boolean>(false);
  outline = input<boolean>(false);
  danger = input<boolean>(false);
  isGhost = input<boolean>(false);

  clicking = output<MouseEvent>();
}
