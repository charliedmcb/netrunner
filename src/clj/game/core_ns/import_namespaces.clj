(in-ns 'game.core)

(import-vars

  [game.core.abilities
   add-cost-label-to-ability
   build-cost-label
   build-cost-string
   build-spend-msg
   can-pay?
   can-trigger?
   cost->string
   cost-name
   cost-ranks
   handler
   is-ability?
   label
   merge-costs
   not-used-once?
   pay
   payable?
   prompt!
   register-ability-type
   register-once
   resolve-ability
   select-ability-kw
   sentence-join
   should-trigger?
   value]

  [game.core.board
   all-active
   all-active-installed
   all-installed
   all-installed-runner-type
   get-all-installed
   get-remote-names
   get-remote-zones
   get-remotes
   get-zones
   in-play?
   installable-servers
   installed-byname
   number-of-virus-counters
   server->zone
   server-list]

  [game.core.card
   active?
   agenda?
   asset?
   assoc-host-zones
   can-be-advanced?
   card-index
   condition-counter?
   corp-installable-type?
   corp?
   event?
   facedown?
   faceup?
   fake-identity?
   get-card
   get-card-hosted
   get-cid
   get-counters
   get-nested-host
   get-zone
   hardware?
   has-subtype?
   ice?
   identity?
   in-archives-root?
   in-current?
   in-deck?
   in-discard?
   in-hand?
   in-hq-root?
   in-play-area?
   in-rd-root?
   in-root?
   in-scored?
   in-server?
   installed?
   is-type?
   map->Card
   operation?
   private-card
   program?
   resource?
   rezzed?
   runner?
   upgrade?
   virus-program?]

  [game.core.card-defs
   card-def]

  [game.core.cost-fns
   break-sub-ability-cost
   card-ability-cost
   has-trash-ability?
   ignore-install-cost?
   install-additional-cost-bonus
   install-cost
   jack-out-cost
   play-additional-cost-bonus
   play-cost
   rez-additional-cost-bonus
   rez-cost
   run-additional-cost-bonus
   run-cost
   trash-cost]

  [game.core.effects
   any-effects
   gather-effects
   get-effects
   register-constant-effects
   register-floating-effect
   sum-effects
   unregister-constant-effects
   unregister-floating-effects]

  [game.core.eid
   complete-with-result
   effect-completed
   eid-set-defaults
   make-eid
   make-result
   register-effect-completed]

  [game.core.events
   ability-as-handler
   card-as-handler
   card-for-ability
   default-locations
   effect-as-handler
   event-count
   event-title
   first-event?
   first-installed-trash-own?
   first-installed-trash?
   first-run-event?
   first-successful-run-on-server?
   first-trash?
   gather-events
   get-installed-trashed
   get-turn-damage
   last-turn?
   log-event
   no-event?
   no-run-event?
   not-last-turn?
   register-events
   register-suppress
   run-event-count
   run-events
   second-event?
   trigger-event
   trigger-event-simult
   trigger-event-sync
   trigger-suppress
   turn-events
   unregister-event-by-uuid
   unregister-events
   unregister-floating-events
   unregister-floating-events-for-card
   unregister-suppress
   unregister-suppress-by-uuid]

  [game.core.finding
   find-card
   find-cid
   find-latest
   get-scoring-owner]

  [game.core.flags
   ab-can-prevent?
   any-flag-fn?
   can-access-loud
   can-access?
   can-advance?
   can-host?
   can-rez?
   can-run-server?
   can-run?
   can-score?
   can-steal?
   can-trash?
   card-can-prevent?
   card-flag-fn?
   card-flag?
   card-is-public?
   cards-can-prevent?
   check-flag-types?
   clear-all-flags-for-card!
   clear-persistent-flag!
   clear-run-flag!
   clear-run-register!
   clear-turn-flag!
   clear-turn-register!
   enable-run-on-server
   get-card-prevention
   get-prevent-list
   get-preventing-cards
   has-flag?
   in-corp-scored?
   in-runner-scored?
   is-scored?
   lock-zone
   persistent-flag?
   prevent-current
   prevent-draw
   prevent-jack-out
   prevent-run-on-server
   register-persistent-flag!
   register-run-flag!
   register-turn-flag!
   release-zone
   run-flag?
   turn-flag?
   untrashable-while-resources?
   untrashable-while-rezzed?
   when-scored?
   zone-locked?]

  [game.core.ice
   add-extra-sub!
   add-sub
   add-sub!
   all-subs-broken-by-card?
   all-subs-broken?
   any-subs-broken-by-card?
   any-subs-broken?
   auto-icebreaker
   break-all-subroutines
   break-all-subroutines!
   break-sub
   break-subroutine
   break-subroutine!
   break-subroutines
   break-subroutines-msg
   breakable-subroutines-choice
   breaker-strength
   dont-resolve-all-subroutines
   dont-resolve-all-subroutines!
   dont-resolve-subroutine
   dont-resolve-subroutine!
   get-current-ice
   get-strength
   ice-strength
   pump
   pump-all-ice
   pump-all-icebreakers
   pump-ice
   remove-extra-subs!
   remove-sub
   remove-sub!
   remove-subs
   remove-subs!
   reset-all-ice
   reset-all-subs
   reset-all-subs!
   reset-sub
   reset-sub!
   resolve-subroutine
   resolve-subroutine!
   resolve-unbroken-subs!
   strength-pump
   sum-ice-strength-effects
   unbroken-subroutines-choice
   update-all-ice
   update-all-icebreakers
   update-breaker-strength
   update-ice-in-server
   update-ice-strength]

  [game.core.player
   map->Corp
   map->Runner
   new-corp
   new-runner]

  [game.core.prompts
   choice-parser
   clear-wait-prompt
   resolve-select
   show-prompt
   show-prompt-with-dice
   show-select
   show-trace-prompt
   show-wait-prompt]

  [game.core.say
   enforce-msg
   indicate-action
   play-sfx
   say
   system-msg
   system-say
   typing
   typingstop]

  [game.core.state
   make-rid
   map->State
   new-state]

  [game.core.to-string
   card-str
   name-zone]

  [game.core.toasts
   show-error-toast
   toast]

  [game.core.update
   update!
   update-hosted!]

  [game.macros
   continue-ability
   effect
   msg
   req
   wait-for
   when-let*])
