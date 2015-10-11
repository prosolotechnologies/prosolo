var paging = {
	create : function(set) {

		function pages(set, paging) {
			return Math.floor(set.length / paging) + (set.length % paging > 0 ? 1 : 0);
		}

		function page(set, page, paging) {
			var count = pages(set, paging);
			if (page > count || page == 0) {
				return [];
			}
			var start = (page - 1) * paging;
			var end = (page * paging >= set.length) ? set.length : page * paging;
			return {
				"pages" : count,
				"page" : page,
				"result" : set.slice(start, end)
			};
		}
		
		var paging = 5;
		var value = page(set, 1, paging);
		
		return {
			current : function() {
				return value;
			},
			previous : function() {
				var result = page(set, value.page - 1, paging);
				if (result == []) return value;
				value = result;
				return result;
			},
			next : function() {
				var result = page(set, value.page + 1, paging);
				if (result == []) return value;
				value = result;
				return result;
			}
		}
	}
}