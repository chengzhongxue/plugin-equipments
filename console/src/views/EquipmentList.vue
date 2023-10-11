<script lang="ts" setup>
import { computed, nextTick, ref, watch } from "vue";
import {
  VButton,
  VCard,
  VPageHeader,
  VPagination,
  VSpace,
  Dialog,
  VEmpty,
  IconAddCircle,
  VLoading,
  IconCheckboxFill,
  Toast,
  IconArrowLeft,
  IconArrowRight,
  VDropdown,
  VDropdownItem,
} from "@halo-dev/components";
import GroupList from "../components/GroupList.vue";
import EquipmentEditingModal from "@/components/EquipmentEditingModal.vue";
import LazyImage from "@/components/LazyImage.vue";
import apiClient from "@/utils/api-client";
import type { Equipment, EquipmentList } from "@/types";
import Fuse from "fuse.js";
import IconComputer  from "~icons/ri/computer-line";
import type { AttachmentLike } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";

const selectedEquipment = ref<Equipment | undefined>();
const selectedEquipments = ref<Set<Equipment>>(new Set<Equipment>());
const selectedGroup = ref<string>();
const editingModal = ref(false);
const checkedAll = ref(false);
const groupListRef = ref();

const page = ref(1);
const size = ref(20);
const total = ref(0);
const searchText = ref("");
const keyword = ref("");

const {
  data: equipments,
  isLoading,
  refetch,
} = useQuery<Equipment[]>({
  queryKey: [page, size, keyword, selectedGroup],
  queryFn: async () => {
    if (!selectedGroup.value) {
      return [];
    }
    const { data } = await apiClient.get<EquipmentList>(
      "/apis/api.plugin.halo.run/v1alpha1/plugins/PluginEquipments/equipments",
      {
        params: {
          page: page.value,
          size: size.value,
          keyword: keyword.value,
          group: selectedGroup.value,
        },
      }
    );
    total.value = data.total;
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
  refetchOnWindowFocus: false,
});

const handleSelectPrevious = () => {
  if (!equipments.value) {
    return;
  }

  const currentIndex = equipments.value.findIndex(
    (equipment) => equipment.metadata.name === selectedEquipment.value?.metadata.name
  );

  if (currentIndex > 0) {
    selectedEquipment.value = equipments.value[currentIndex - 1];
    return;
  }

  if (currentIndex <= 0) {
    selectedEquipment.value = undefined;
  }
};

const handleSelectNext = () => {
  if (!equipments.value) {
    return;
  }

  if (!selectedEquipment.value) {
    selectedEquipment.value = equipments.value[0];
    return;
  }
  const currentIndex = equipments.value.findIndex(
    (equipment) => equipment.metadata.name === selectedEquipment.value?.metadata.name
  );
  if (currentIndex !== equipments.value.length - 1) {
    selectedEquipment.value = equipments.value[currentIndex + 1];
  }
};

const handleOpenEditingModal = (equipment?: Equipment) => {
  selectedEquipment.value = equipment;
  editingModal.value = true;
};

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "是否确认删除所选的装备？",
    description: "删除之后将无法恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        const promises = Array.from(selectedEquipments.value).map((equipment) => {
          return apiClient.delete(
            `/apis/core.halo.run/v1alpha1/equipments/${equipment.metadata.name}`
          );
        });
        await Promise.all(promises);
      } catch (e) {
        console.error(e);
      } finally {
        pageRefetch();
      }
    },
  });
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  handleCheckAll(checked);
};

const handleCheckAll = (checkAll: boolean) => {
  if (checkAll) {
    equipments.value?.forEach((equipment) => {
      selectedEquipments.value.add(equipment);
    });
  } else {
    selectedEquipments.value.clear();
  }
};

const isChecked = (equipment: Equipment) => {
  return (
    equipment.metadata.name === selectedEquipment.value?.metadata.name ||
    Array.from(selectedEquipments.value)
      .map((item) => item.metadata.name)
      .includes(equipment.metadata.name)
  );
};

watch(
  () => selectedEquipments.value.size,
  (newValue) => {
    checkedAll.value = newValue === equipments.value?.length;
  }
);

// search
let fuse: Fuse<Equipment> | undefined = undefined;

watch(
  () => equipments.value,
  () => {
    if (!equipments.value) {
      return;
    }

    fuse = new Fuse(equipments.value, {
      keys: [
        "spec.displayName",
        "metadata.name",
        "spec.description",
        "spec.url",
      ],
      useExtendedSearch: true,
    });
  }
);

const searchResults = computed({
  get() {
    if (!fuse || !keyword.value) {
      return equipments.value || [];
    }

    return fuse?.search(keyword.value).map((item) => item.item);
  },
  set(value) {
    equipments.value = value;
  },
});

// create by attachments
const attachmentModal = ref(false);

const onAttachmentsSelect = async (attachments: AttachmentLike[]) => {
  const equipments: {
    url: string;
    cover?: string;
    displayName?: string;
    type?: string;
  }[] = attachments
    .map((attachment) => {
      const post = {
        groupName: selectedGroup.value || "",
      };

      if (typeof attachment === "string") {
        return {
          ...post,
          url: attachment,
          cover: attachment,
        };
      }
      if ("url" in attachment) {
        return {
          ...post,
          url: attachment.url,
          cover: attachment.url,
        };
      }
      if ("spec" in attachment) {
        return {
          ...post,
          url: attachment.status?.permalink,
          cover: attachment.status?.permalink,
          displayName: attachment.spec.displayName,
          type: attachment.spec.mediaType,
        };
      }
    })
    .filter(Boolean) as {
    url: string;
    cover?: string;
    displayName?: string;
    type?: string;
  }[];

  for (const equipment of equipments) {
    const type = equipment.type;
    if (!type) {
      Toast.error("只支持选择装备");
      nextTick(() => {
        attachmentModal.value = true;
      });

      return;
    }
    const fileType = type.split("/")[0];
    if (fileType !== "image") {
      Toast.error("只支持选择装备");
      nextTick(() => {
        attachmentModal.value = true;
      });
      return;
    }
  }

  const createRequests = equipments.map((equipment) => {
    return apiClient.post<Equipment>("/apis/core.halo.run/v1alpha1/equipments", {
      metadata: {
        name: "",
        generateName: "equipment-",
      },
      spec: equipment,
      kind: "Equipment",
      apiVersion: "core.halo.run/v1alpha1",
    });
  });

  await Promise.all(createRequests);

  Toast.success(`新建成功，一共创建了 ${equipments.length} 件装备。`);
  pageRefetch();
};

const groupSelectHandle = (group?: string) => {
  selectedGroup.value = group;
};

const pageRefetch = async () => {
  await groupListRef.value.refetch();
  await refetch();
  selectedEquipments.value = new Set<Equipment>();
};
</script>
<template>
  <EquipmentEditingModal
    v-model:visible="editingModal"
    :equipment="selectedEquipment"
    :group="selectedGroup"
    @close="refetch()"
    @saved="pageRefetch"
  >
    <template #append-actions>
      <span @click="handleSelectPrevious">
        <IconArrowLeft />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight />
      </span>
    </template>
  </EquipmentEditingModal>
  <AttachmentSelectorModal
    v-model:visible="attachmentModal"
    :accepts="['image/*']"
    @select="onAttachmentsSelect"
  />
  <VPageHeader title="我的装备">
    <template #icon>
      <IconComputer class="equipments-mr-2 equipments-self-center" />
    </template>
  </VPageHeader>
  <div class="equipments-p-4">
    <div class="equipments-flex equipments-flex-col equipments-gap-2 sm:equipments-flex-row">
      <div class="equipments-w-full sm:equipments-w-80">
        <GroupList ref="groupListRef" @select="groupSelectHandle" />
      </div>
      <div class="equipments-flex-1">
        <VCard>
          <template #header>
            <div
              class="equipments-block equipments-w-full equipments-bg-gray-50 equipments-px-4 equipments-py-3"
            >
              <div
                class="equipments-relative equipments-flex equipments-flex-col equipments-items-start sm:equipments-flex-row sm:equipments-items-center"
              >
                <div
                  class="equipments-mr-4 equipments-hidden equipments-items-center sm:equipments-flex"
                >
                  <input
                    v-model="checkedAll"
                    class="equipments-h-4 equipments-w-4 equipments-rounded equipments-border-gray-300 equipments-text-indigo-600"
                    type="checkbox"
                    @change="handleCheckAllChange"
                  />
                </div>
                <div
                  class="equipments-flex equipments-w-full equipments-flex-1 sm:equipments-w-auto"
                >
                  <FormKit
                    v-if="!selectedEquipments.size"
                    v-model="searchText"
                    placeholder="输入关键词搜索"
                    type="text"
                    @keyup.enter="keyword = searchText"
                  ></FormKit>
                  <VSpace v-else>
                    <VButton type="danger" @click="handleDeleteInBatch">
                      删除
                    </VButton>
                  </VSpace>
                </div>
                <div
                  v-if="selectedGroup"
                  v-permission="['plugin:equipments:manage']"
                  class="equipments-mt-4 equipments-flex sm:equipments-mt-0"
                >
                  <VDropdown>
                    <VButton size="xs"> 新增 </VButton>
                    <template #popper>
                      <VDropdownItem @click="handleOpenEditingModal()">
                        新增
                      </VDropdownItem>
                      <VDropdownItem @click="attachmentModal = true">
                        从附件库选择
                      </VDropdownItem>
                    </template>
                  </VDropdown>
                </div>
              </div>
            </div>
          </template>
          <VLoading v-if="isLoading" />
          <Transition v-else-if="!selectedGroup" appear name="fade">
            <VEmpty message="请选择或新建分组" title="未选择分组"></VEmpty>
          </Transition>
          <Transition v-else-if="!searchResults.length" appear name="fade">
            <VEmpty message="你可以尝试刷新或者新建装备" title="当前没有装备">
              <template #actions>
                <VSpace>
                  <VButton @click="refetch"> 刷新</VButton>
                  <VButton
                    v-permission="['plugin:equipments:manage']"
                    type="primary"
                    @click="handleOpenEditingModal()"
                  >
                    <template #icon>
                      <IconAddCircle class="h-full w-full" />
                    </template>
                    新增装备
                  </VButton>
                </VSpace>
              </template>
            </VEmpty>
          </Transition>
          <Transition v-else appear name="fade">
            <div
              class="equipments-mt-2 equipments-grid equipments-grid-cols-1 equipments-gap-x-2 equipments-gap-y-3 sm:equipments-grid-cols-2 md:equipments-grid-cols-3 lg:equipments-grid-cols-4"
              role="list"
            >
              <VCard
                v-for="equipment in equipments"
                :key="equipment.metadata.name"
                :body-class="['!p-0']"
                :class="{
                  'equipments-ring-primary equipments-ring-1': isChecked(equipment),
                  'equipments-ring-1 equipments-ring-red-600':
                    equipment.metadata.deletionTimestamp,
                }"
                class="hover:equipments-shadow"
                @click="handleOpenEditingModal(equipment)"
              >
                <div class="equipments-group equipments-relative equipments-bg-white">
                  <div
                    class="equipments-aspect-w-10 equipments-aspect-h-8 equipments-block equipments-h-full equipments-w-full equipments-cursor-pointer equipments-overflow-hidden equipments-bg-gray-100"
                  >
                    <LazyImage
                      :key="equipment.metadata.name"
                      :alt="equipment.spec.displayName"
                      :src="equipment.spec.cover || equipment.spec.url"
                      classes="equipments-w-full equipments-h-40 equipments-pointer-events-none equipments-object-cover group-hover:equipments-opacity-75"
                    >
                      <template #loading>
                        <div
                          class="equipments-flex equipments-h-full equipments-items-center equipments-justify-center equipments-object-cover"
                        >
                          <span class="equipments-text-xs equipments-text-gray-400"
                            >加载中...</span
                          >
                        </div>
                      </template>
                      <template #error>
                        <div
                          class="equipments-flex equipments-h-full equipments-items-center equipments-justify-center equipments-object-cover"
                        >
                          <span class="equipments-text-xs equipments-text-red-400">
                            加载异常
                          </span>
                        </div>
                      </template>
                    </LazyImage>
                  </div>

                  <p
                    v-tooltip="equipment.spec.displayName"
                    class="equipments-block equipments-cursor-pointer equipments-truncate equipments-px-2 equipments-py-1 equipments-text-center equipments-text-xs equipments-font-medium equipments-text-gray-700"
                  >
                    {{ equipment.spec.displayName }}
                  </p>

                  <div
                    v-if="equipment.metadata.deletionTimestamp"
                    class="equipments-absolute equipments-top-1 equipments-right-1 equipments-text-xs equipments-text-red-300"
                  >
                    删除中...
                  </div>

                  <div
                    v-if="!equipment.metadata.deletionTimestamp"
                    v-permission="['plugin:equipments:manage']"
                    :class="{ '!flex': selectedEquipments.has(equipment) }"
                    class="equipments-absolute equipments-top-0 equipments-left-0 equipments-hidden equipments-h-1/3 equipments-w-full equipments-cursor-pointer equipments-justify-end equipments-bg-gradient-to-b equipments-from-gray-300 equipments-to-transparent equipments-ease-in-out group-hover:equipments-flex"
                    @click.stop="
                      selectedEquipments.has(equipment)
                        ? selectedEquipments.delete(equipment)
                        : selectedEquipments.add(equipment)
                    "
                  >
                    <IconCheckboxFill
                      :class="{
                        '!text-primary': selectedEquipments.has(equipment),
                      }"
                      class="hover:equipments-text-primary equipments-mt-1 equipments-mr-1 equipments-h-6 equipments-w-6 equipments-cursor-pointer equipments-text-white equipments-transition-all"
                    />
                  </div>
                </div>
              </VCard>
            </div>
          </Transition>

          <template #footer>
            <div
              class="equipments-flex equipments-items-center equipments-justify-end equipments-bg-white"
            >
              <div
                class="equipments-flex equipments-flex-1 equipments-items-center equipments-justify-end"
              >
                <VPagination
                  v-model:page="page"
                  v-model:size="size"
                  :total="total"
                  :size-options="[20, 30, 50, 100]"
                />
              </div>
            </div>
          </template>
        </VCard>
      </div>
    </div>
  </div>
</template>
