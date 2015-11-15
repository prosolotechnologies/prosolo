define([], function() {
	/*
	{
		"container" : "#table-container",
		"rows" : {
			"class" : "data-row-css-class"
		},
		"columns" : [
				{
					"name" : "column-data-name",
					"title" : "column-title",
					"type" : "text", 		 // text, button, checkbox
					"key" : "true", 		 // data value is in row's data set 
					"value" : "Disable",	 // applies to button type
					"click" : function() {}  // button's click event
					"change" : function() {} // checkbox's change event
				} ] // ,...
	}
	*/
	
	return {
		create : function(configuration) {
			var columns = configuration.columns;
			var container = configuration.container;
	
			function th(column) {
				var th = document.createElement("th");
				th.innerHTML = column.title;
				th.dataset["name"] = column.name;
				return th;
			}
	
			var table = document.querySelector(container + " table");
			table.innerHTML = "";
			var thead = document.createElement("thead");
			var tr = document.createElement("tr");
	
			configuration.columns.map(function(column) {
				tr.appendChild(th(column));
			});
	
			thead.appendChild(tr);
			table.appendChild(thead);
	
			return {
				init : function(data) {
					if (document.querySelector(container + " table tbody")) {
						document.querySelector(container + " table tbody").remove();	
					}
					function td(value) {
						var td = document.createElement("td");
						td.innerHTML = value;
						return td;
					}
	
					var tbody = document.createElement("tbody");
	
					data.map(function(item) {
						var tr = document.createElement("tr");
						tr.classList.add(configuration.rows["class"]);
						configuration.columns.map(function(column) {
							var td = document.createElement("td");
							if (column.key == "true") {
								tr.dataset[column.name] = item[column.name];
							}
							if (column.type == "text") {
								td.innerHTML = item[column.name];
							} else if (column.type == "button") {
								var button = document.createElement("button");
								button.addEventListener("click", column.click);
								button.innerHTML = column.value;
								td.appendChild(button);
							} else if (column.type == "checkbox") {
								var checkbox = document.createElement("input");
								checkbox.setAttribute("type", "checkbox");
								checkbox.addEventListener("change", column.change);
								td.appendChild(checkbox);
							}
							tr.appendChild(td);
						});
						tbody.appendChild(tr);
					});
					table.appendChild(tbody);
				}
			}
		}
	}
});