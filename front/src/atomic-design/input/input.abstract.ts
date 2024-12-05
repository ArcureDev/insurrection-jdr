import {
  Component,
  effect,
  input,
  model,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormControlComponent } from './form-control.abstract';

export type InputType =
  | 'text'
  | 'color'
  | 'email'
  | 'date'
  | 'datetime-local'
  | 'tel'
  | 'number'
  | 'password'
  | 'url';

@Component({
  template: '',
  standalone: true,
})
export class InputAbstractComponent<T> extends FormControlComponent<T> {
  id = model<string | undefined>();
  label = input.required<string>();
  placeholder = input<string | undefined>();
  inputType = model.required<InputType>();

  title = signal<string | undefined>(undefined);

  constructor() {
    super();

    effect(() => {
      if (this.id()) return;
      this.id.set(Math.random().toString());
    });

    effect(() => {
      this.title.set(this.label());
    });
  }
}
