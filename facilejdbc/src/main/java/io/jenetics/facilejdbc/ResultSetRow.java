/*
 * Facile JDBC Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.facilejdbc;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class ResultSetRow implements Row {

	private final ResultSet _rs;

	private ResultSetRow(final ResultSet result) {
		_rs = requireNonNull(result);
	}

	@Override
	public boolean wasNull() throws SQLException {
		return _rs.wasNull();
	}

	@Override
	public String getString(final int columnIndex) throws SQLException {
		return _rs.getString(columnIndex);
	}

	@Override
	public boolean getBoolean(final int columnIndex) throws SQLException {
		return _rs.getBoolean(columnIndex);
	}

	@Override
	public byte getByte(final int columnIndex) throws SQLException {
		return _rs.getByte(columnIndex);
	}

	@Override
	public short getShort(final int columnIndex) throws SQLException {
		return _rs.getShort(columnIndex);
	}

	@Override
	public int getInt(final int columnIndex) throws SQLException {
		return _rs.getInt(columnIndex);
	}

	@Override
	public long getLong(final int columnIndex) throws SQLException {
		return _rs.getLong(columnIndex);
	}

	@Override
	public float getFloat(final int columnIndex) throws SQLException {
		return _rs.getFloat(columnIndex);
	}

	@Override
	public double getDouble(final int columnIndex) throws SQLException {
		return _rs.getDouble(columnIndex);
	}

	@Override
	public byte[] getBytes(final int columnIndex) throws SQLException {
		return _rs.getBytes(columnIndex);
	}

	@Override
	public Date getDate(final int columnIndex) throws SQLException {
		return _rs.getDate(columnIndex);
	}

	@Override
	public Time getTime(final int columnIndex) throws SQLException {
		return _rs.getTime(columnIndex);
	}

	@Override
	public Timestamp getTimestamp(final int columnIndex) throws SQLException {
		return _rs.getTimestamp(columnIndex);
	}

	@Override
	public InputStream getAsciiStream(final int columnIndex)
		throws SQLException
	{
		return _rs.getAsciiStream(columnIndex);
	}

	@Override
	public InputStream getBinaryStream(final int columnIndex)
		throws SQLException
	{
		return _rs.getBinaryStream(columnIndex);
	}

	@Override
	public String getString(final String columnLabel) throws SQLException {
		return _rs.getString(columnLabel);
	}

	@Override
	public boolean getBoolean(final String columnLabel) throws SQLException {
		return _rs.getBoolean(columnLabel);
	}

	@Override
	public byte getByte(final String columnLabel) throws SQLException {
		return _rs.getByte(columnLabel);
	}

	@Override
	public short getShort(final String columnLabel) throws SQLException {
		return _rs.getShort(columnLabel);
	}

	@Override
	public int getInt(final String columnLabel) throws SQLException {
		return _rs.getInt(columnLabel);
	}

	@Override
	public long getLong(final String columnLabel) throws SQLException {
		return _rs.getLong(columnLabel);
	}

	@Override
	public float getFloat(final String columnLabel) throws SQLException {
		return _rs.getFloat(columnLabel);
	}

	@Override
	public double getDouble(final String columnLabel) throws SQLException {
		return _rs.getDouble(columnLabel);
	}

	@Override
	public byte[] getBytes(final String columnLabel) throws SQLException {
		return _rs.getBytes(columnLabel);
	}

	@Override
	public Date getDate(final String columnLabel) throws SQLException {
		return _rs.getDate(columnLabel);
	}

	@Override
	public Time getTime(final String columnLabel) throws SQLException {
		return _rs.getTime(columnLabel);
	}

	@Override
	public Timestamp getTimestamp(final String columnLabel) throws SQLException {
		return _rs.getTimestamp(columnLabel);
	}

	@Override
	public InputStream getAsciiStream(final String columnLabel)
		throws SQLException
	{
		return _rs.getAsciiStream(columnLabel);
	}

	@Override
	public InputStream getBinaryStream(final String columnLabel)
		throws SQLException
	{
		return _rs.getBinaryStream(columnLabel);
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return _rs.getMetaData();
	}

	@Override
	public Object getObject(final int columnIndex) throws SQLException {
		return _rs.getObject(columnIndex);
	}

	@Override
	public Object getObject(final String columnLabel) throws SQLException {
		return _rs.getObject(columnLabel);
	}

	@Override
	public int findColumn(final String columnLabel) throws SQLException {
		return _rs.findColumn(columnLabel);
	}

	@Override
	public Reader getCharacterStream(final int columnIndex)
		throws SQLException
	{
		return _rs.getCharacterStream(columnIndex);
	}

	@Override
	public Reader getCharacterStream(final String columnLabel)
		throws SQLException
	{
		return _rs.getCharacterStream(columnLabel);
	}

	@Override
	public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
		return _rs.getBigDecimal(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(final String columnLabel)
		throws SQLException
	{
		return _rs.getBigDecimal(columnLabel);
	}

	@Override
	public int getRow() throws SQLException {
		return _rs.getRow();
	}

	@Override
	public int getType() throws SQLException {
		return _rs.getType();
	}

	@Override
	public Object getObject(
		final int columnIndex,
		final Map<String, Class<?>> map
	)
		throws SQLException
	{
		return _rs.getObject(columnIndex, map);
	}

	@Override
	public Ref getRef(final int columnIndex) throws SQLException {
		return _rs.getRef(columnIndex);
	}

	@Override
	public Blob getBlob(final int columnIndex) throws SQLException {
		return _rs.getBlob(columnIndex);
	}

	@Override
	public Clob getClob(final int columnIndex) throws SQLException {
		return _rs.getClob(columnIndex);
	}

	@Override
	public Array getArray(final int columnIndex) throws SQLException {
		return _rs.getArray(columnIndex);
	}

	@Override
	public Object getObject(
		final String columnLabel,
		final Map<String, Class<?>> map
	)
		throws SQLException
	{
		return _rs.getObject(columnLabel, map);
	}

	@Override
	public Ref getRef(final String columnLabel) throws SQLException {
		return _rs.getRef(columnLabel);
	}

	@Override
	public Blob getBlob(final String columnLabel) throws SQLException {
		return _rs.getBlob(columnLabel);
	}

	@Override
	public Clob getClob(final String columnLabel) throws SQLException {
		return _rs.getClob(columnLabel);
	}

	@Override
	public Array getArray(final String columnLabel) throws SQLException {
		return _rs.getArray(columnLabel);
	}

	@Override
	public Date getDate(final int columnIndex, final Calendar cal)
		throws SQLException
	{
		return _rs.getDate(columnIndex, cal);
	}

	@Override
	public Date getDate(final String columnLabel, final Calendar cal)
		throws SQLException
	{
		return _rs.getDate(columnLabel, cal);
	}

	@Override
	public Time getTime(final int columnIndex, final Calendar cal)
		throws SQLException
	{
		return _rs.getTime(columnIndex, cal);
	}

	@Override
	public Time getTime(final String columnLabel, final Calendar cal)
		throws SQLException
	{
		return _rs.getTime(columnLabel, cal);
	}

	@Override
	public Timestamp getTimestamp(final int columnIndex, final Calendar cal)
		throws SQLException
	{
		return _rs.getTimestamp(columnIndex, cal);
	}

	@Override
	public Timestamp getTimestamp(final String columnLabel, final Calendar cal)
		throws SQLException
	{
		return _rs.getTimestamp(columnLabel, cal);
	}

	@Override
	public URL getURL(final int columnIndex) throws SQLException {
		return _rs.getURL(columnIndex);
	}

	@Override
	public URL getURL(final String columnLabel) throws SQLException {
		return _rs.getURL(columnLabel);
	}

	@Override
	public RowId getRowId(final int columnIndex) throws SQLException {
		return _rs.getRowId(columnIndex);
	}

	@Override
	public RowId getRowId(final String columnLabel) throws SQLException {
		return _rs.getRowId(columnLabel);
	}

	@Override
	public NClob getNClob(final int columnIndex) throws SQLException {
		return _rs.getNClob(columnIndex);
	}

	@Override
	public NClob getNClob(final String columnLabel) throws SQLException {
		return _rs.getNClob(columnLabel);
	}

	@Override
	public SQLXML getSQLXML(final int columnIndex) throws SQLException {
		return _rs.getSQLXML(columnIndex);
	}

	@Override
	public SQLXML getSQLXML(final String columnLabel) throws SQLException {
		return _rs.getSQLXML(columnLabel);
	}

	@Override
	public String getNString(final int columnIndex) throws SQLException {
		return _rs.getNString(columnIndex);
	}

	@Override
	public String getNString(final String columnLabel) throws SQLException {
		return _rs.getNString(columnLabel);
	}

	@Override
	public Reader getNCharacterStream(final int columnIndex)
		throws SQLException
	{
		return _rs.getNCharacterStream(columnIndex);
	}

	@Override
	public Reader getNCharacterStream(final String columnLabel)
		throws SQLException
	{
		return _rs.getNCharacterStream(columnLabel);
	}

	@Override
	public <T> T getObject(final int columnIndex, final Class<T> type)
		throws SQLException
	{
		return _rs.getObject(columnIndex, type);
	}

	@Override
	public <T> T getObject(final String columnLabel, final Class<T> type)
		throws SQLException
	{
		return _rs.getObject(columnLabel, type);
	}

	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		return _rs.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		return _rs.isWrapperFor(iface);
	}

	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	static ResultSetRow of(final ResultSet result) {
		return new ResultSetRow(result);
	}

}
