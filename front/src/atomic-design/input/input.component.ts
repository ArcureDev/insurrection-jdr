import { Component, forwardRef, OnChanges } from '@angular/core';
import {
  FormsModule,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { InputAbstractComponent } from './input.abstract';

@Component({
  selector: 'ins-input',
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: './input.component.html',
  styleUrl: './input.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true,
    },
    {
      provide: NG_VALIDATORS,
      multi: true,
      useExisting: InputComponent,
    },
  ],
})
export class InputComponent extends InputAbstractComponent<string> {}
