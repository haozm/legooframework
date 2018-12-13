package com.legooframework.model.customer.entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import com.google.common.base.Preconditions;

public class CustomerConsumptionEntity {
	
	private Set<CustomerId> customerIds;
	
	private List<Map<String,Object>> datas;
	
	CustomerConsumptionEntity(Set<CustomerId> customerIds,List<Map<String,Object>> datas){
		Preconditions.checkArgument(CollectionUtils.isNotEmpty(customerIds),"入参customerIds不能为空或null");
		Objects.requireNonNull(datas,"入参datas不能为null");
		this.customerIds = customerIds;
		this.datas = datas;
	}
	
	
	public Optional<Map<String,Object>> getCustomerItem(CustomerEntity customer){
		Objects.requireNonNull(customer,"入参customer不能为空");
		return datas.stream().filter( x -> {
			Long id = MapUtils.getLong(x, "id");
			Long storeId = MapUtils.getLong(x, "store_id");
			int channel = MapUtils.getIntValue(x, "account_type");
			CustomerId customerId = new CustomerId(id, Channel.valueOf(channel), storeId);
			return customerId.equals(customer.getId());
		}).findAny();
	}
	
	public Optional<List<Map<String,Object>>> getCustomerStatistics(CustomerEntity customer){
		Objects.requireNonNull(customer,"入参customer不能为空");
		return null;
	}
	
	public Optional<List<Map<String,Object>>> getAllItems(){
		return null;
	}
	
	public Optional<List<Map<String,Object>>> getAllCount(){
		return null;
	}
	
	
}
