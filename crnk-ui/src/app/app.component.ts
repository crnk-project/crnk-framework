import {Component, ElementRef, AfterViewInit} from "@angular/core";


@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.scss']
})
export class AppComponent implements AfterViewInit {
	title = 'app works!';

	constructor(private el: ElementRef) {
	}

	ngAfterViewInit() {
	}
}
