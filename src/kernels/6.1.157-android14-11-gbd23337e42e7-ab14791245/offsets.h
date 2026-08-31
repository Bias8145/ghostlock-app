/*
 * Device: Google Pixel 8 (shiba)
 * Kernel: 6.1.157-android14-11-gbd23337e42e7-ab14791245
 */

OFFSETS_ENTRY(
    "6.1.157-android14-11-gbd23337e42e7-ab14791245",
    STRUCT_OFFSETS_6_1,

    /* Derived from Ghostlock App extraction log */
    .pselect_waiter_shift = 1,

    /* Extracted via nm from vmlinux.elf */
    .off_init_task            = 0x0201F640,
    .off_init_cred            = 0x02031AA8,
    .off_root_task_group      = 0x02208580,
    .off_selinux_enforcing    = 0x01FCA8C0,
    .off_selinux_blob_sizes   = 0x015CEB88,
    .off_security_hook_heads  = 0x015CE478,

    /* Derived from Ghostlock App extraction log */
    .off_slide_nfulnl_logger  = 0x020129D0,

    /* Extracted via nm from vmlinux.elf (sysctl_bootid symbol) */
    .off_slide_boot_id        = 0x0227B498,

    /* Derived from Ghostlock App extraction log */
    .off_slide_loggers_0_1    = 0x02012918
),

/*
 * BTF reference – runtime uses target.h defaults:
 * #define STRUCT_PAGE_SIZE           0x40
 * #define STRUCT_PAGE_COMPOUND_HEAD  0x8
 * #define STRUCT_PAGE_TYPE           0x30
 * #define STRUCT_SLAB_CACHE          0x8
 * #define STRUCT_MM_STRUCT           0x4C0
 */
