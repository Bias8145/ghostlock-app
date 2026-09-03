/* 6.6.87-android15-8-g88d70c0b44bf-ab14541233-4k: Motorola G57 Power 5G (Mumba), */

OFFSETS_ENTRY(
    "6.6.87-android15-8-g88d70c0b44bf-ab14541233-4k",
    STRUCT_OFFSETS_6_6,
    .pselect_waiter_shift = -2,
    .off_init_task = 0x020DEB40,
    .off_init_cred = 0x020F0F08,
    .off_root_task_group = 0x022D2A00,
    .off_selinux_enforcing = 0x02314520,
    .off_selinux_blob_sizes = 0x0164A7C8,
    .off_security_hook_heads = 0x0164A110,
    .off_slide_nfulnl_logger = 0x020D2258,
    .off_slide_boot_id = 0x02337D30,
    .off_slide_loggers_0_1 = 0x020D21A8,
),

/* BTF reference for fields kept as target.h macros; the task, cred and
 * pi field offsets ride STRUCT_OFFSETS_6_1 at runtime: */
/* #define STRUCT_PAGE_SIZE 0x40 */
/* #define STRUCT_PAGE_COMPOUND_HEAD 0x8 */
/* #define STRUCT_PAGE_TYPE 0x30 */
/* #define STRUCT_SLAB_CACHE 0x18 */
/* #define STRUCT_MM_STRUCT 0x3C0 */
