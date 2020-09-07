/******************************************************************************
 * Copyright (C) 2009 Low Heng Sin                                            *
 * Copyright (C) 2009 Idalica Corporation                                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.idempiere.faaguilar.webui.form;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;
import java.util.logging.Level;

import org.compiere.apps.IStatusBar;
import org.compiere.grid.CreateFrom;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.GridTab;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MRequisitionLine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;

/**
 * 
 * @author Fabian Aguilar faaguilar@gmail.com
 *
 */
public abstract class CreateFromOrder extends CreateFrom
{
	/**
	 *  Protected Constructor
	 *  @param mTab MTab
	 */
	public CreateFromOrder(GridTab mTab)
	{
		super(mTab);
		if (log.isLoggable(Level.INFO)) log.info(mTab.toString());
	}   //  VCreateFromInvoice

	/**
	 *  Dynamic Init
	 *  @return true if initialized
	 */
	public boolean dynInit() throws Exception
	{
		log.config("");
		setTitle(Msg.getElement(Env.getCtx(), "C_Order_ID", false) + " .. " + Msg.translate(Env.getCtx(), "CreateFrom"));

		return true;
	}   //  dynInit


	/**
	 *  Load Data - Shipment not invoiced
	 *  @param M_InOut_ID InOut
	 */
	protected Vector<Vector<Object>> getRequisitionData(Object Requisition, Object Org,  Object User)
	{
		

		//
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		StringBuilder sql = new StringBuilder("select r.M_Requisition_ID,r.DocumentNo,r.DateRequired,r.PriorityRule,rl.M_Product_ID,");   //1-5
				sql.append(" p.Name as ProductName,rl.Description,rl.Qty,rl.C_BPartner_ID, bp.Name as BpName, rl.M_RequisitionLine_ID, u.Name as Username, o.Name as OrgName,");  //6-13
				sql.append(" c.C_Charge_ID, c.name as ChargeName");// 14-15
				sql.append(" from M_Requisition r " );
				sql.append(" inner join M_RequisitionLine rl on (r.m_requisition_id=rl.m_requisition_id)" );
				sql.append(" inner join AD_User u on (r.AD_User_ID=u.AD_User_ID)" );
				sql.append(" inner join AD_Org o on (r.AD_Org_ID=o.AD_Org_ID)" );
				sql.append(" left outer join M_Product p on (rl.M_Product_ID=p.M_Product_ID)" );
				sql.append(" left outer join C_Charge c on (rl.C_Charge_ID=c.C_Charge_ID)" );
				sql.append(" left outer join C_BPartner bp on (rl.C_BPartner_ID=bp.C_BPartner_ID)" );
				sql.append(" where r.docstatus='CO' and rl.C_OrderLine_ID is null");
		
		if(Requisition!=null)
			sql.append(" AND rl.M_Requisition_ID=?");
		if(Org!=null)
			sql.append(" AND r.AD_Org_ID=?");
		if(User!=null)
			sql.append(" AND r.AD_User_ID=?");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			int i=1;
			pstmt = DB.prepareStatement(sql.toString(), null);
			if(Requisition!=null)
				pstmt.setInt(i++, (Integer)Requisition);
			if(Org!=null)
				pstmt.setInt(i++, (Integer)Org);
			if(User!=null)
				pstmt.setInt(i++, (Integer)User);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				Vector<Object> line = new Vector<Object>(7);
				line.add(new Boolean(false));           //  0-Selection
				line.add(rs.getString(13)); //1-OrgName
				
				KeyNamePair pp =  new KeyNamePair(rs.getInt(11), rs.getString(2).trim());
				line.add(pp);  //  2-DocumentNo Line iD
				line.add(rs.getTimestamp(3));//  3-DateRequired
				
				if(rs.getString(10)!=null)
				{
					pp = new KeyNamePair(rs.getInt(9), rs.getString(10).trim());
					line.add(pp);	//  4-BPartner
				}
				else
					line.add(null); //  4-BPartner
			
				if( rs.getString(6)!=null)
				{
				pp = new KeyNamePair(rs.getInt(5), rs.getString(6).trim());
				line.add(pp);				// 5-Product
				}
				else
				line.add(null);      // 5-Product
				
				if(rs.getString(15)!=null)
					line.add(rs.getString(15)); //6-charge
				else
					line.add(null); //6-charge
				
				line.add(rs.getBigDecimal(8));//7- Qty
		
				line.add(rs.getString(7));                           //  8-description
				line.add(rs.getString(12).trim());                     	//  9-user
				data.add(line);
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

		return data;
	}   //  loadShipment


	/**
	 *  List number of rows selected
	 */
	public void info(IMiniTable miniTable, IStatusBar statusBar)
	{

	}   //  infoInvoice

	protected void configureMiniTable (IMiniTable miniTable)
	{
		miniTable.setColumnClass(0, Boolean.class, false);      //  0-Selection
		miniTable.setColumnClass(1, String.class, true);        //  1-OrgName
		miniTable.setColumnClass(2, String.class, true);        //  2-DocumentNo
		miniTable.setColumnClass(3, Timestamp.class, true);        //  3-DateRequired
		miniTable.setColumnClass(4, String.class, true);        //  4-BPartner
		miniTable.setColumnClass(5, String.class, true);        //  5-Product
		miniTable.setColumnClass(6, String.class, true);        //  6-Charge
		miniTable.setColumnClass(7, BigDecimal.class, true);        //  7-Qty
		miniTable.setColumnClass(8, String.class, true);        //  8-Description
		miniTable.setColumnClass(9, String.class, true);        //  9-User
		
		//  Table UI
		miniTable.autoSize();
	}

	/**
	 *  Save - Create Invoice Lines
	 *  @return true if saved
	 */
	public boolean save(IMiniTable miniTable, String trxName)
	{
		//  Order
		int C_Order_ID = ((Integer)getGridTab().getValue("C_Order_ID")).intValue();
		MOrder order = new MOrder (Env.getCtx(), C_Order_ID, trxName);
		if (log.isLoggable(Level.CONFIG)) log.config(order.toString());

		//  Lines
		for (int i = 0; i < miniTable.getRowCount(); i++)
		{
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue())
			{
				

				KeyNamePair pp = (KeyNamePair)miniTable.getValueAt(i, 2);   //  1-documentno  line id
				int M_RequisitionLine_ID = pp.getKey();
				MRequisitionLine rLine = new MRequisitionLine (Env.getCtx(), M_RequisitionLine_ID, trxName);

				//	Create new Order Line
				MOrderLine m_orderLine = new MOrderLine (order);
				m_orderLine.setDatePromised(rLine.getDateRequired());
				if (rLine.getM_Product_ID() >0)
				{
					m_orderLine.setProduct(MProduct.get(Env.getCtx(), rLine.getM_Product_ID()));
					m_orderLine.setM_AttributeSetInstance_ID(rLine.getM_AttributeSetInstance_ID());
				}
				else
				{
					m_orderLine.setC_Charge_ID(rLine.getC_Charge_ID());
					
				}
				m_orderLine.setPriceActual(rLine.getPriceActual());
				m_orderLine.setAD_Org_ID(rLine.getAD_Org_ID());
				m_orderLine.setQty(rLine.getQty());
				m_orderLine.saveEx();
				
				//	Update Requisition Line
				rLine.setC_OrderLine_ID(m_orderLine.getC_OrderLine_ID());
				rLine.saveEx();
				
			}   //   if selected
		}   //  for all rows

		

		return true;
	}   //  saveInvoice

	protected Vector<String> getOISColumnNames()
	{
		//  Header Info
	    Vector<String> columnNames = new Vector<String>(7);
	    columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
	    columnNames.add(Msg.getElement(Env.getCtx(), "AD_Org_ID"));
	    columnNames.add(Msg.translate(Env.getCtx(), "Documentno"));
	    columnNames.add(Msg.translate(Env.getCtx(), "DateRequired"));
	    columnNames.add(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
	    columnNames.add(Msg.getElement(Env.getCtx(), "M_Product_ID", false));
	    columnNames.add(Msg.getElement(Env.getCtx(), "C_Charge_ID", false));
	    columnNames.add(Msg.getElement(Env.getCtx(), "Qty"));
	    columnNames.add(Msg.getElement(Env.getCtx(), "Description", false));
	    columnNames.add(Msg.getElement(Env.getCtx(), "AD_User_ID", false));
	    

	    return columnNames;
	}

}
