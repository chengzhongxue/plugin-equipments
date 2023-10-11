import "./styles/tailwind.css";
import { definePlugin } from "@halo-dev/console-shared";
import EquipmentList from "@/views/EquipmentList.vue";
import { markRaw } from "vue";
import { IconComputer }  from "@halo-dev/components";

export default definePlugin({
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/equipments",
        name: "Equipments",
        component: EquipmentList,
        meta: {
          permissions: ["plugin:equipments:view"],
          menu: {
            name: "我的装备",
            group: "content",
            icon: markRaw(IconComputer),
          },
        },
      },
    },
  ],
});
