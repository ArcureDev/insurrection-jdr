import {
  AbstractControl,
  FormControl,
  FormGroup,
  ValidationErrors,
} from '@angular/forms';
import { isBlank } from './string.utils';

export function notBlankValidator(
  control: AbstractControl,
): ValidationErrors | null {
  const isValid = !isBlank(control.value);
  return isValid ? null : { blank: true };
}

export function samePasswordsValidator(
  group: AbstractControl<
    FormGroup<{ password: FormControl; confirmationPassword: FormControl }>
  >,
): ValidationErrors | null {
  const pass = group.get('password')?.value;
  const confirmPass = group.get('passwordConfirmation')?.value;
  return pass === confirmPass ? null : { notSame: true };
}
