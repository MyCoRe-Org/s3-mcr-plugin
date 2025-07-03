<script setup lang="ts">
import { onMounted } from 'vue';
import { Tooltip } from 'bootstrap';

interface Props {
  title: string;
  target: string;
  placement: string;
  triggers: string;
}
const props = defineProps<Props>();

onMounted(() => {
  const target = document.getElementById(props.target);
  if (target) {
    new Tooltip(target, {
      trigger: props.triggers,
      placement: props.placement,
      title: props.title,
    } as Tooltip.Options);
  }
});
</script>
