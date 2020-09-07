package org.idempiere.faaguilar.webui.factory;

import org.compiere.grid.ICreateFrom;
import org.compiere.grid.ICreateFromFactory;
import org.compiere.model.GridTab;
import org.compiere.model.I_C_Order;
import org.idempiere.faaguilar.webui.form.WCreateFromOrderUI;

/**
 * 
 * @author Fabian Aguilar faaguilar@gmail.com
 *
 */
public class FaaCreateFromFactory implements ICreateFromFactory {
	@Override
	public ICreateFrom create(GridTab mTab) 
	{
		String tableName = mTab.getTableName();
		if (tableName.equals(I_C_Order.Table_Name))
			return new WCreateFromOrderUI(mTab);
		
		return null;
	}
}
