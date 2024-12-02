import {
  AbstractControl,
  ControlValueAccessor,
  ValidationErrors,
  Validator,
} from '@angular/forms';
import { Component, input, Input, signal } from '@angular/core';
import { isNotNullOrUndefined } from '../../app/utils/object.utils';

// eslint-disable-next-line no-unused-vars
type FunctionType<T> = (t?: T) => void;

@Component({
  template: '',
  standalone: false,
})
export abstract class FormControlComponent<T>
  implements ControlValueAccessor, Validator
{
  /**
   * ControlValueAccessor part
   *
   * Add this to component for a custom form control :
   *     {
   *       provide: NG_VALUE_ACCESSOR,
   *       multi: true,
   *       useExisting: MyComponent
   *     }
   **/

  @Input() isTouched = false;
  @Input() invalid = false;
  isFormSubmitted = input<boolean>(false);
  value = signal<T | undefined>(undefined);
  disabled = false;

  onTouchedReactive: FunctionType<never> = () => {
    this.isTouched = true;
  };

  onChange: FunctionType<T> = () => {
    /* empty */
  };

  onTouched: FunctionType<T> = () => {
    /* empty */
  };

  onValidator: FunctionType<T> = () => {};

  registerOnChange(fn: FunctionType<T>): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: FunctionType<T>): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  writeValue(obj: T): void {
    this.value.set(obj);
  }

  /**
   * Validator part
   *
   * Add this to component for a custom validation :
   *     {
   *       provide: NG_VALIDATORS,
   *       multi: true,
   *       useExisting: MyComponent
   *     }
   **/

  registerOnValidatorChange(fn: () => void) {
    this.onValidator = fn;
  }

  validate(control: AbstractControl): ValidationErrors | null {
    if (!this.isFormSubmitted()) return null;
    this.invalid = isNotNullOrUndefined(control.errors);
    return control.errors;
  }
}
