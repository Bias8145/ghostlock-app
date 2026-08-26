/* Generated offsets for 6.6.118-android15-8-gbf8cd367de7a-ab15314822-4k. */

#define STRUCT_OFFSETS_EXTRACTED \
  .task_prio = 0x84, \
  .task_normal_prio = 0x8C, \
  .task_sched_task_group = 0x348, \
  .task_pi_lock = 0x90C, \
  .task_pi_waiters = 0x920, \
  .task_pi_top_task = 0x930, \
  .task_pi_blocked_on = 0x938, \
  .task_pid = 0x618, \
  .task_tgid = 0x61C, \
  .task_atomic_flags = 0x5D8, \
  .task_real_cred = 0x818, \
  .task_cred = 0x820, \
  .task_comm = 0x830, \
  .task_tasks = 0x550, \
  .task_seccomp = 0x8E8,

OFFSETS_ENTRY("6.6.118-android15-8-gbf8cd367de7a-ab15314822-4k",
  STRUCT_OFFSETS_6_6,
  .pselect_waiter_shift=-2,
  .off_init_task=0x0212E280,
  .off_init_cred=0x02140748,
  .off_root_task_group=0x02328980,
  .off_selinux_enforcing=0x0236A2E0,
  .off_selinux_blob_sizes=0x016849F0,
  .off_security_hook_heads=0x016842B8,
  .off_slide_nfulnl_logger=0x02122260,
  .off_slide_boot_id=0x0238B2D8,
  .off_slide_loggers_0_1=0x021221B0,
),

/* BTF fields not stored in kernel_offsets: */
#define WAITER_TREE 0x0
#define WAITER_PI_TREE 0x28
#define WAITER_TASK 0x50
#define WAITER_LOCK 0x58
#define WAITER_WAKE_STATE 0x60
#define WAITER_WW_CTX 0x68
#define CRED_UID 0x8
#define CRED_SECUREBITS 0x28
#define CRED_CAPS 0x30
#define CRED_SECURITY 0x80
#define SECCOMP_MODE 0x0
#define SECCOMP_FILTER_COUNT 0x4
#define SECCOMP_FILTER 0x8
#define STRUCT_PAGE_SIZE 0x40
#define STRUCT_PAGE_COMPOUND_HEAD 0x8
#define STRUCT_PAGE_TYPE 0x30
#define STRUCT_SLAB_CACHE 0x8
