import { Component } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registered-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './registered-home.component.html',
  styleUrls: ['./registered-home.component.css']
})
export class RegisteredHomeComponent {
  travelForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.travelForm = this.fb.group({
      destinations: this.fb.array([
        this.createDestination('Starting point'),  // start
        this.createDestination('Destination')      // end
      ])
    });
  }

  get destinations(): FormArray {
    return this.travelForm.get('destinations') as FormArray;
  }

  createDestination(defaultLabel: string = ''): FormGroup {
    return this.fb.group({
      name: ['', Validators.required]
    });
  }

  addDestination(index: number) {
    this.destinations.insert(index + 1, this.createDestination(''));
  }

  removeDestination(index: number) {
    if (this.destinations.length > 2) {
      this.destinations.removeAt(index);
    }
  }

  submit() {
    if (this.travelForm.valid) {
      console.log('Destinations:', this.travelForm.value.destinations);
    }
  }
}
