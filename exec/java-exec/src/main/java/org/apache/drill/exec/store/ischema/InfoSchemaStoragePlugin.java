/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.store.ischema;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import net.hydromatic.optiq.SchemaPlus;

import org.apache.drill.common.JSONOptions;
import org.apache.drill.common.logical.StoragePluginConfig;
import org.apache.drill.exec.planner.logical.DrillTable;
import org.apache.drill.exec.rpc.user.DrillUser;
import org.apache.drill.exec.server.DrillbitContext;
import org.apache.drill.exec.store.AbstractSchema;
import org.apache.drill.exec.store.AbstractStoragePlugin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class InfoSchemaStoragePlugin extends AbstractStoragePlugin{
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InfoSchemaStoragePlugin.class);

  private final InfoSchemaConfig config;
  private final DrillbitContext context;
  private final String name;

  public InfoSchemaStoragePlugin(InfoSchemaConfig config, DrillbitContext context, String name){
    this.config = config;
    this.context = context;
    this.name = name;
  }

  @Override
  public boolean supportsRead() {
    return true;
  }

  @Override
  public InfoSchemaGroupScan getPhysicalScan(JSONOptions selection) throws IOException {
    SelectedTable table = selection.getWith(context.getConfig(),  SelectedTable.class);
    return new InfoSchemaGroupScan(table);
  }

  @Override
  public StoragePluginConfig getConfig() {
    return this.config;
  }

  @Override
  public void registerSchemas(DrillUser user, SchemaPlus parent) {
    ISchema s = new ISchema(parent, this);
    parent.add(s.getName(), s);
  }

  private class ISchema extends AbstractSchema{
    private Map<String, InfoSchemaDrillTable> tables;
    public ISchema(SchemaPlus parent, InfoSchemaStoragePlugin plugin){
      super("INFORMATION_SCHEMA");
      Map<String, InfoSchemaDrillTable> tbls = Maps.newHashMap();
      for(SelectedTable tbl : SelectedTable.values()){
        tbls.put(tbl.name(), new InfoSchemaDrillTable(plugin, "INFORMATION_SCHEMA", tbl, config));
      }
      this.tables = ImmutableMap.copyOf(tbls);
    }

    @Override
    public DrillTable getTable(String name) {
      return tables.get(name);
    }

    @Override
    public Set<String> getTableNames() {
      return tables.keySet();
    }

  }
}
