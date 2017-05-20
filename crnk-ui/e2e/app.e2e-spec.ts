import {MoapMovieMgmtCliPage} from "./app.po";

describe('moap-movie-mgmt-cli App', function () {
	let page: MoapMovieMgmtCliPage;

	beforeEach(() => {
		page = new MoapMovieMgmtCliPage();
	});

	it('should display message saying app works', () => {
		page.navigateTo();
		expect(page.getParagraphText()).toEqual('app works!');
	});
});
