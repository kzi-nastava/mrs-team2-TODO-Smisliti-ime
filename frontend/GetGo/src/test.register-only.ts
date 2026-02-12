// Test entry that only loads the RegisterComponent spec
// Load Zone.js first
import 'zone.js';
import 'zone.js/testing';

import { getTestBed } from '@angular/core/testing';

// Require the platform-browser testing entry at runtime to avoid module resolution issues
const pbTesting: any = require('@angular/platform-browser/testing');

getTestBed().initTestEnvironment(pbTesting.BrowserTestingModule, pbTesting.platformBrowserTesting());

// Load only the register spec
// The path is relative to src/
declare const require: any;
require('./app/pages/authentication/register/register.spec.ts');
