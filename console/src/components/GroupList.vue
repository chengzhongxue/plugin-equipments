<script lang="ts" setup>
import {
  VButton,
  VCard,
  VEntity,
  IconList,
  VEntityField,
  VStatusDot,
  Dialog,
  VEmpty,
  VLoading,
  VDropdownItem,
} from "@halo-dev/components";
import GroupEditingModal from "./GroupEditingModal.vue";
import type { EquipmentGroup } from "@/types";
import type { EquipmentGroupList } from "@/types";
import { ref } from "vue";
import Draggable from "vuedraggable";
import apiClient from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import { useQuery } from "@tanstack/vue-query";

const emit = defineEmits<{
  (event: "select", group?: string): void;
}>();

const loading = ref(false);
const groupEditingModal = ref(false);

const updateGroup = ref<EquipmentGroup>();

const selectedGroup = useRouteQuery<string>("equipment-group");

const { data: groups, refetch } = useQuery<EquipmentGroup[]>({
  queryKey: [],
  queryFn: async () => {
    const { data } = await apiClient.get<EquipmentGroupList>(
      "/apis/api.plugin.halo.run/v1alpha1/plugins/PluginEquipments/equipmentgroups"
    );
    return data.items
      .map((group) => {
        if (group.spec) {
          group.spec.priority = group.spec.priority || 0;
        }
        return group;
      })
      .sort((a, b) => {
        return (a.spec?.priority || 0) - (b.spec?.priority || 0);
      });
  },
  refetchInterval(data) {
    const deletingGroups = data?.filter(
      (group) => !!group.metadata.deletionTimestamp
    );

    return deletingGroups?.length ? 1000 : false;
  },
  onSuccess(data) {
    if (selectedGroup.value) {
      const groupNames = data.map((group) => group.metadata.name);
      if (groupNames.includes(selectedGroup.value)) {
        emit("select", selectedGroup.value);
        return;
      }
    }

    if (data.length) {
      handleSelectedClick(data[0]);
    } else {
      selectedGroup.value = "";
      emit("select", "");
    }
  },
  refetchOnWindowFocus: false,
});

const handleSaveInBatch = async () => {
  try {
    const promises = groups.value?.map((group: EquipmentGroup, index) => {
      if (group.spec) {
        group.spec.priority = index;
      }
      return apiClient.put(
        `/apis/core.halo.run/v1alpha1/equipmentgroups/${group.metadata.name}`,
        group
      );
    });
    if (promises) {
      await Promise.all(promises);
    }
  } catch (e) {
    console.error(e);
  } finally {
    refetch();
  }
};

const handleDelete = async (group: EquipmentGroup) => {
  Dialog.warning({
    title: "确定要删除该分组吗？",
    description: "将同时删除该分组下的所有装备，该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await apiClient.delete(
          `/apis/api.plugin.halo.run/v1alpha1/plugins/PluginEquipments/equipmentgroups/${group.metadata.name}`
        );
        refetch();
      } catch (e) {
        console.error("Failed to delete equipment group", e);
      }
    },
  });
};

const handleOpenEditingModal = (group?: EquipmentGroup) => {
  groupEditingModal.value = true;
  updateGroup.value = group;
};

const handleSelectedClick = (group: EquipmentGroup) => {
  selectedGroup.value = group.metadata.name;
  emit("select", group.metadata.name);
};

defineExpose({
  refetch,
});
</script>
<template>
  <GroupEditingModal
    v-model:visible="groupEditingModal"
    :group="updateGroup"
    @close="refetch()"
  />
  <VCard :body-class="['!p-0']" title="分组">
    <VLoading v-if="loading" />
    <Transition v-else-if="!groups || !groups.length" appear name="fade">
      <VEmpty message="你可以尝试刷新或者新建分组" title="当前没有分组">
        <template #actions>
          <VSpace>
            <VButton size="sm" @click="refetch()"> 刷新</VButton>
          </VSpace>
        </template>
      </VEmpty>
    </Transition>
    <Transition v-else appear name="fade">
      <Draggable
        v-model="groups"
        class="equipments-box-border equipments-h-full equipments-w-full equipments-divide-y equipments-divide-gray-100"
        group="group"
        handle=".drag-element"
        item-key="metadata.name"
        tag="ul"
        @change="handleSaveInBatch"
      >
        <template #item="{ element: group }">
          <li @click="handleSelectedClick(group)">
            <VEntity
              :is-selected="selectedGroup === group.metadata.name"
              class="equipments-group"
            >
              <template #prepend>
                <div
                  class="drag-element equipments-absolute equipments-inset-y-0 equipments-left-0 equipments-hidden equipments-w-3.5 equipments-cursor-move equipments-items-center equipments-bg-gray-100 equipments-transition-all hover:equipments-bg-gray-200 group-hover:equipments-flex"
                >
                  <IconList class="h-3.5 w-3.5" />
                </div>
              </template>

              <template #start>
                <VEntityField
                  :title="group.spec?.displayName"
                  :description="`${group.status.equipmentCount || 0} 个装备`"
                ></VEntityField>
              </template>

              <template #end>
                <VEntityField v-if="group.metadata.deletionTimestamp">
                  <template #description>
                    <VStatusDot v-tooltip="`删除中`" state="warning" animate />
                  </template>
                </VEntityField>
              </template>

              <template #dropdownItems>
                <VDropdownItem @click="handleOpenEditingModal(group)">
                  修改
                </VDropdownItem>
                <VDropdownItem type="danger" @click="handleDelete(group)">
                  删除
                </VDropdownItem>
              </template>
            </VEntity>
          </li>
        </template>
      </Draggable>
    </Transition>

    <template v-if="!loading" #footer>
      <Transition appear name="fade">
        <VButton
          v-permission="['plugin:equipments:manage']"
          block
          type="secondary"
          @click="handleOpenEditingModal(undefined)"
        >
          新增分组
        </VButton>
      </Transition>
    </template>
  </VCard>
</template>
