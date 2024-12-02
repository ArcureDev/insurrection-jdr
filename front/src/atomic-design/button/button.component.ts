import { Component } from '@angular/core';
import { AbstractButtonComponent } from './abstract-button.component';
import { NgClass } from '@angular/common';

@Component({
  selector: 'ins-button',
  imports: [NgClass],
  templateUrl: './button.component.html',
  styleUrls: ['./abstract-button.component.scss', './button.component.scss'],
})
export class ButtonComponent extends AbstractButtonComponent {}
