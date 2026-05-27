

Yes — there’s solid support for organizing code by **feature/function** in deeper directory structures, while keeping type-based grouping only at the top level when it helps orient the repo.[^1]

## Strong supporting material

- **Structure by feature vs. type**: This article explicitly contrasts a type-based layout like `controllers/`, `models/`, `validators/` with a feature-based layout like `auth/controller.js`, `auth/model.js`, `auth/validator.js`, and concludes that feature-based structure scales better in larger codebases.[^1]
- **Hybrid structure**: The same source argues for a hybrid approach where the high-level structure can reflect types or application areas, but the deeper structure groups by feature so the folder tree “tells a story” about the app.[^1]
- **Feature-focused code organization**: Another article recommends organizing code around the products and features the system actually has, not around technical layers, because that improves navigation and keeps related code together.[^2]
- **Package-by-feature**: A Java-oriented source states that packages should reflect a feature set and place all items related to a single feature into one directory/package, which aligns closely with your view.[^3]


## Counterpoint you may want

There is also a respected opposing view that argues for mostly flat directory structures and says folders often add less value than editor search and symbol navigation. That’s useful as a balancing reference, but it doesn’t negate feature-oriented organization for larger or more modular systems.[^4][^1]

## Best citations to use

If you want a compact set of sources to support your position, these are the strongest:

- **Structure by Type vs Feature** — good practical explanation and examples[^1]
- **The Life-changing (And Time-saving!) Magic Of Feature-Focused Code Organization** — clear advocacy for feature-based organization[^2]
- **Java Practices: Package-by-feature, not layer** — concise statement of the principle for Java code[^3]
- **A Front-End Application Folder Structure that Makes Sense** — gives a concrete hybrid pattern with feature folders and shared top-level structure[^5]

I can also turn these into a short internal-style argument or a set of bullet-point talking points for a code review or architecture discussion.
<span style="display:none">[^10][^11][^12][^13][^14][^15][^16][^17][^18][^19][^20][^21][^22][^23][^24][^25][^26][^27][^28][^29][^6][^7][^8][^9]</span>

<div align="center">⁂</div>

[^1]: https://dev.to/jesterxl/code-organization-in-functional-programming-vs-object-oriented-programming-79i

[^2]: https://dev.to/jamesmh/the-life-changing-and-time-saving-magic-of-feature-focused-code-organization-1708

[^3]: http://www.javapractices.com/topic/TopicAction.do?Id=205

[^4]: https://www.reddit.com/r/functionalprogramming/comments/1b3ncbj/functional_in_oop_code_base/

[^5]: https://fadamakis.com/a-front-end-application-folder-structure-that-makes-sense-ecc0b690968b

[^6]: https://www.aviator.co/blog/how-to-manage-code-in-a-large-codebase/

[^7]: https://slack.engineering/happiness-is-a-freshly-organized-codebase/

[^8]: https://stackoverflow.com/questions/7278200/approach-to-designing-large-functional-programs

[^9]: https://stackoverflow.com/questions/583826/how-should-source-code-files-be-organized-by-function-or-type

[^10]: https://algocademy.com/blog/the-ultimate-guide-to-structuring-and-organizing-code-projects-for-maximum-efficiency/

[^11]: https://www.functionalnoise.com/pages/2022-12-29-org-refactor/

[^12]: https://chrisfrew.in/blog/advanced-design-patterns-the-case-for-one-function-per-file/

[^13]: https://blog.ploeh.dk/2023/05/29/favour-flat-code-file-folders/

[^14]: https://www.freecodecamp.org/news/organizing-code-with-functions/

[^15]: https://bartfokker.com/posts/scaling-source-code/

[^16]: https://maestros.io/structure-by-type-vs-feature

[^17]: https://www.linkedin.com/posts/petarivanovv9_most-react-codebases-dont-get-messy-because-activity-7442553297179369472-n3H2

[^18]: https://www.youtube.com/watch?v=3f4g8RwELC4

[^19]: https://www.reddit.com/r/node/comments/1n6ejzo/project_structure_help_me_understand_folder_by/

[^20]: https://dev.to/sathishskdev/part-2-folder-structure-building-a-solid-foundation-omh

[^21]: https://dev.to/hxnain619/why-i-switched-to-a-feature-based-folder-structure-and-why-you-should-too-3lpo

[^22]: https://stackoverflow.com/questions/3320753/how-to-group-compare-similar-news-articles

[^23]: https://www.iteratorshq.com/blog/a-comprehensive-guide-on-project-folder-organization/

[^24]: https://doc.atlasti.com/QuicktourWin/Codes/CodeSystem.html

[^25]: https://www.linkedin.com/posts/laxu_stop-your-codebase-from-turning-into-a-junk-activity-7359890730036748289-i4BE

[^26]: https://helpdesk.dedoose.com/hc/en-us/articles/12905367765133-Collaborative-Code-and-Compare

[^27]: https://www.reddit.com/r/reactjs/comments/18qkhgi/folder_structure_group_by_feature_vs_group_by/

[^28]: https://dev.to/mfp22/why-your-folder-structure-sucks-2jb4

[^29]: https://www.linkedin.com/posts/thibault-friedrich_start-with-feature-based-architecture-it-activity-7386004359995768832-iFS9

