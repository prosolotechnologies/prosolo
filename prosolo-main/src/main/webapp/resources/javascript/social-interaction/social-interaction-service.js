var socialInteractionService = (function() {
	function flatten(arrays) {
		return [].concat.apply([], arrays);
	}

	function clusterInteraction(item, interaction) {
		return {
			course: item.courseid,
			source: {
				student: item.student,
				cluster: item.clusterid
			},
			target: {
				student: interaction.target,
				cluster: item.clusterid
			},
			count: interaction.count,
			avatar: item.avatar,
			name: item.name	
		};
	}

	function outerInteraction(item, interaction, isSource) {
		var a = {
			student: item.student,
			cluster: item.clusterid
		};
		var b = {
			student: interaction.target,
			cluster: interaction.clusterid
		};
		return {
			course: item.courseid,
			source: isSource ? a : b,
			target: isSource ? b : a,
			count: interaction.count,
			avatar: item.avatar,
			name: item.name	
		};
	}

	function denormalize(clusterInteractions, outerInteractions) {
		return flatten([
			flatten(outerInteractions.map(function(item) {
				return item.interactions.map(function(interaction) {
					var isSource = item.direction === "source";
					return outerInteraction(item, interaction, isSource);
				});
			})),
			flatten(clusterInteractions.map(function(item) {
				return item.interactions.map(function(interaction) {
					return clusterInteraction(item, interaction);
				});
			}))
		]);
	}

	return {
		denormalize: denormalize
	};

})();
