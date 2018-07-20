app.controller('baseController',function($scope){
	
	//分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法 
	$scope.paginationConf = {
		currentPage: 1,
		totalItems: 10,
		itemsPerPage: 10,
		perPageOptions: [10, 20, 30, 40, 50],
		onChange: function(){
			$scope.reloadList();
		}
	};
	
	//刷新列表
	$scope.reloadList=function(){
		$scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}
	
	$scope.selectIds=[];//选中的ID集合 
	//更新复选
	$scope.updateSelection = function($event, id) {		
			if($event.target.checked){//如果是被选中,则增加到数组
					$scope.selectIds.push( id);			
			}else{
					var idx = $scope.selectIds.indexOf(id);
			         $scope.selectIds.splice(idx, 1);//参数1：移除的位置，参数2：移除的个数
			}
	}
	
	$scope.jsonToString=function(jsonString,key){
		
		var json = JSON.parse(jsonString);
		var value = "";
		for(var i=0;i<json.length;i++){
			if(i>0){
				value += ",";
			}
			value += json[i][key];
		}
		
		return value;
	}
	
});