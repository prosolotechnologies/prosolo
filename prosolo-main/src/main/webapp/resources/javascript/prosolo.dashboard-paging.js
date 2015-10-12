var paging = {
	create : function(set, paging) {
		function empty() {
			return {
				"pages" : 0,
				"page" : 0,
				"size" : 0,
				"result" : []
			};
		}
		
		function pages(set, paging) {
			var size = set.length;
			return Math.floor(size / paging) + (size % paging > 0 ? 1 : 0);
		}

		function page(set, page, paging) {
			var pagesCount = pages(set, paging);
			if (page > pagesCount|| page == 0) {
				return empty();
			}
			var start = (page - 1) * paging;
			var end = (page * paging < set.length) ? page * paging : set.length;
			return {
				"pages" : pagesCount,
				"page" : page,
				"size" : set.length,
				"result" : set.slice(start, end)
			};
		}
		
		var value = page(set, 1, paging);
		
		return {
			current : function() {
				return value;
			},
			previous : function() {
				if (value.page == 1) return value;
				value = page(set, value.page - 1, paging);
				return value;
			},
			next : function() {
				if (value.page == value.pages) return value;
				value = page(set, value.page + 1, paging);
				return value;
			},
			page : function(number) {
				if (number == 0) {
					value = page(set, 1, paging);
				} else if (number > value.pages) {
					value = page(set, value.pages, paging);
				} else {
					value = page(set, number, paging);
				}
				return value;
			}
		}
	}
}