import {FormArray, FormBuilder, FormControl} from '@angular/forms';

export function fillFormArray<T>(
  formBuilder: FormBuilder,
  formArray: FormArray<FormControl<T>>, values: T[]
) {
  values.forEach(value => {
    formArray.push(formBuilder.nonNullable.control(value));
  })
}