// Test setup - load only Register spec to avoid loading other specs
import 'zone.js';
import 'zone.js/testing';

import { getTestBed } from '@angular/core/testing';

// Require platform-browser testing at runtime to avoid static ESM export issues
const pbTesting: any = require('@angular/platform-browser/testing');

getTestBed().initTestEnvironment(pbTesting.BrowserTestingModule, pbTesting.platformBrowserTesting());

// Prevent Karma from running tests until Angular test environment is set up
declare const require: any;

// Load only the register spec
require('./app/pages/authentication/register/register.spec.ts');
